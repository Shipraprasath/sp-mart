/**
 * SP MART — Complete Interactive Client-Side Script
 * B.Tech AI & Data Science Capstone Project
 */

document.addEventListener('DOMContentLoaded', () => {

    // 1. Resilient Image Fallback (SVG Tech Graphic)
    const placeholderSvg = "data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%22400%22%20height%3D%22400%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20viewBox%3D%220%200%20400%20400%22%3E%3Crect%20width%3D%22400%22%20height%3D%22400%22%20fill%3D%22%23F5F7FB%22%2F%3E%3Ccircle%20cx%3D%22200%22%20cy%3D%22180%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23287BFF%22%20stroke-width%3D%228%22%2F%3E%3Cpath%20d%3D%22M170%20270h60M200%20230v40%22%20stroke%3D%22%23287BFF%22%20stroke-width%3D%228%22%20stroke-linecap%3D%22round%22%2F%3E%3Ctext%20x%3D%22200%22%20y%3D%22330%22%20text-anchor%3D%22middle%22%20font-family%3D%22sans-serif%22%20font-size%3D%2218%22%20font-weight%3D%22bold%22%20fill%3D%22%23172033%22%3ESP%20MART%3C%2Ftext%3E%3C%2Fsvg%3E";

    document.querySelectorAll('img').forEach(img => {
        img.addEventListener('error', () => {
            img.src = placeholderSvg;
        });
    });

    // 2. Password Toggle Visibility
    document.querySelectorAll('[data-toggle-password]').forEach(button => {
        button.addEventListener('click', () => {
            const targetId = button.getAttribute('data-toggle-password');
            const targetInput = document.getElementById(targetId);
            if (targetInput) {
                if (targetInput.type === 'password') {
                    targetInput.type = 'text';
                    button.textContent = 'Hide';
                } else {
                    targetInput.type = 'password';
                    button.textContent = 'Show';
                }
            }
        });
    });

    // 3. Form Double-Submit Protection
    document.querySelectorAll('form[data-prevent-double-submit]').forEach(form => {
        form.addEventListener('submit', () => {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = `<span>Processing...</span>`;
            }
        });
    });

    // 4. Quantity Stepper Controls
    document.querySelectorAll('.qty-btn-plus').forEach(btn => {
        btn.addEventListener('click', () => {
            const input = btn.closest('.qty-control-wrapper')?.querySelector('.qty-input');
            if (input) {
                const max = parseInt(input.getAttribute('max') || '99', 10);
                const current = parseInt(input.value || '1', 10);
                if (current < max) {
                    input.value = current + 1;
                }
            }
        });
    });

    document.querySelectorAll('.qty-btn-minus').forEach(btn => {
        btn.addEventListener('click', () => {
            const input = btn.closest('.qty-control-wrapper')?.querySelector('.qty-input');
            if (input) {
                const min = parseInt(input.getAttribute('min') || '1', 10);
                const current = parseInt(input.value || '1', 10);
                if (current > min) {
                    input.value = current - 1;
                }
            }
        });
    });

    // 5. Auto-Dismiss Alerts
    document.querySelectorAll('.alert-auto-dismiss').forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

    // 6. Interactive AI Shopping Assistant
    const aiToggleBtn = document.getElementById('ai-assistant-toggle');
    const aiModal = document.getElementById('ai-assistant-modal');
    const aiCloseBtn = document.getElementById('ai-assistant-close');
    const aiForm = document.getElementById('ai-chat-form');
    const aiInput = document.getElementById('ai-chat-input');
    const aiMessages = document.getElementById('ai-chat-messages');
    const aiChipsContainer = document.getElementById('ai-chips');

    if (aiToggleBtn && aiModal) {
        aiToggleBtn.addEventListener('click', () => {
            aiModal.classList.toggle('open');
            if (aiModal.classList.contains('open') && aiInput) {
                aiInput.focus();
            }
        });

        if (aiCloseBtn) {
            aiCloseBtn.addEventListener('click', () => {
                aiModal.classList.remove('open');
            });
        }

        const appendUserMessage = (text) => {
            const div = document.createElement('div');
            div.className = 'ai-msg ai-msg-user';
            div.textContent = text;
            aiMessages.appendChild(div);
            aiMessages.scrollTop = aiMessages.scrollHeight;
        };

        const appendAssistantMessage = (reply, products, comparisonMode, productA, productB) => {
            const div = document.createElement('div');
            div.className = 'ai-msg ai-msg-assistant';

            // Format markdown text simply
            let formattedReply = reply
                .replace(/### (.*?)\n/g, '<h4 style="margin: 0.5rem 0; color: #142B4A;">$1</h4>')
                .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
                .replace(/\*(.*?)\*/g, '<em>$1</em>')
                .replace(/\n/g, '<br>');

            div.innerHTML = `<div>${formattedReply}</div>`;

            // If products returned, render interactive product cards
            if (products && products.length > 0) {
                const cardContainer = document.createElement('div');
                cardContainer.style.marginTop = '0.75rem';
                cardContainer.style.display = 'flex';
                cardContainer.style.flexDirection = 'column';
                cardContainer.style.gap = '0.5rem';

                products.forEach(p => {
                    const card = document.createElement('div');
                    card.className = 'ai-card-item';
                    card.innerHTML = `
                        <img src="${p.imageUrl}" alt="${p.name}" class="ai-card-img" onerror="this.src='${placeholderSvg}'">
                        <div class="ai-card-info">
                            <strong style="color: #172033; display: block; font-size: 0.85rem;">${p.name}</strong>
                            <div style="color: #287BFF; font-weight: 700; font-size: 0.9rem;">
                                ₹${Number(p.price).toLocaleString('en-IN')}
                                ${p.discountPercentage > 0 ? `<span style="font-size: 0.7rem; color: #F04452; margin-left: 4px;">(${p.discountPercentage}% OFF)</span>` : ''}
                            </div>
                            <span style="font-size: 0.7rem; color: #687386;">⭐ ${p.rating} · ${p.inStock ? 'In Stock' : 'Out of Stock'}</span>
                        </div>
                        <a href="/products/${p.id}" class="btn btn-primary btn-sm" style="padding: 0.35rem 0.65rem; font-size: 0.75rem; text-decoration: none;">View</a>
                    `;
                    cardContainer.appendChild(card);
                });

                div.appendChild(cardContainer);
            }

            aiMessages.appendChild(div);
            aiMessages.scrollTop = aiMessages.scrollHeight;
        };

        const updateChips = (chips) => {
            if (!aiChipsContainer) return;
            aiChipsContainer.innerHTML = '';
            if (chips && chips.length > 0) {
                chips.forEach(chipText => {
                    const btn = document.createElement('button');
                    btn.type = 'button';
                    btn.className = 'ai-chip';
                    btn.textContent = chipText;
                    btn.addEventListener('click', () => {
                        sendAiQuery(chipText);
                    });
                    aiChipsContainer.appendChild(btn);
                });
            }
        };

        const sendAiQuery = async (queryText) => {
            if (!queryText || queryText.trim() === '') return;
            appendUserMessage(queryText);
            if (aiInput) aiInput.value = '';

            // Show typing indicator
            const typingDiv = document.createElement('div');
            typingDiv.className = 'ai-msg ai-msg-assistant';
            typingDiv.id = 'ai-typing-indicator';
            typingDiv.innerHTML = '<em>Thinking with SP MART catalog intelligence...</em>';
            aiMessages.appendChild(typingDiv);
            aiMessages.scrollTop = aiMessages.scrollHeight;

            try {
                const response = await fetch('/api/ai/assistant/chat', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ message: queryText })
                });

                const data = await response.json();
                typingDiv.remove();

                appendAssistantMessage(data.reply, data.products, data.comparisonMode, data.productA, data.productB);
                updateChips(data.suggestedPrompts);
            } catch (err) {
                if (typingDiv) typingDiv.remove();
                appendAssistantMessage("I'm sorry, I encountered a temporary connection issue. Please feel free to search our catalog or ask another question!", [], false, null, null);
            }
        };

        if (aiForm) {
            aiForm.addEventListener('submit', (e) => {
                e.preventDefault();
                const q = aiInput.value.trim();
                sendAiQuery(q);
            });
        }

        // Attach event listeners to initial chips
        document.querySelectorAll('.ai-chip').forEach(chip => {
            chip.addEventListener('click', () => {
                sendAiQuery(chip.textContent);
            });
        });
    }

    // 7. Newsletter AJAX Subscription
    const newsletterForm = document.getElementById('newsletter-form');
    if (newsletterForm) {
        newsletterForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const emailInput = document.getElementById('newsletter-email');
            const feedbackBox = document.getElementById('newsletter-feedback');
            if (!emailInput || !feedbackBox) return;

            const email = emailInput.value.trim();
            if (!email) return;

            try {
                const res = await fetch('/api/newsletter/subscribe', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: `email=${encodeURIComponent(email)}`
                });
                const data = await res.json();
                feedbackBox.style.display = 'block';
                feedbackBox.className = data.success ? 'alert alert-success' : 'alert alert-danger';
                feedbackBox.textContent = data.message;
                if (data.success) {
                    emailInput.value = '';
                }
            } catch (err) {
                feedbackBox.style.display = 'block';
                feedbackBox.className = 'alert alert-danger';
                feedbackBox.textContent = 'Unable to subscribe at this moment. Please try again.';
            }
        });
    }

    // 8. Order Confirmation Web Audio Chime with Mute Option
    const confirmationBanner = document.getElementById('order-confirmation-banner');
    if (confirmationBanner) {
        const isMuted = localStorage.getItem('spmart_sound_muted') === 'true';
        const soundToggle = document.getElementById('sound-toggle-btn');

        const updateMuteUi = (muted) => {
            if (soundToggle) {
                soundToggle.innerHTML = muted ? '🔇 Unmute Chime' : '🔔 Mute Chime';
            }
        };
        updateMuteUi(isMuted);

        if (soundToggle) {
            soundToggle.addEventListener('click', () => {
                const currentMute = localStorage.getItem('spmart_sound_muted') === 'true';
                const newMute = !currentMute;
                localStorage.setItem('spmart_sound_muted', newMute ? 'true' : 'false');
                updateMuteUi(newMute);
                if (!newMute) {
                    playSuccessChime();
                }
            });
        }

        const playSuccessChime = () => {
            try {
                const AudioContext = window.AudioContext || window.webkitAudioContext;
                if (!AudioContext) return;
                const ctx = new AudioContext();

                // Note 1 (E5 - 659 Hz)
                const osc1 = ctx.createOscillator();
                const gain1 = ctx.createGain();
                osc1.type = 'sine';
                osc1.frequency.setValueAtTime(659.25, ctx.currentTime);
                gain1.gain.setValueAtTime(0.2, ctx.currentTime);
                gain1.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.6);
                osc1.connect(gain1);
                gain1.connect(ctx.destination);
                osc1.start(ctx.currentTime);
                osc1.stop(ctx.currentTime + 0.6);

                // Note 2 (A5 - 880 Hz)
                const osc2 = ctx.createOscillator();
                const gain2 = ctx.createGain();
                osc2.type = 'sine';
                osc2.frequency.setValueAtTime(880.00, ctx.currentTime + 0.2);
                gain2.gain.setValueAtTime(0.25, ctx.currentTime + 0.2);
                gain2.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.9);
                osc2.connect(gain2);
                gain2.connect(ctx.destination);
                osc2.start(ctx.currentTime + 0.2);
                osc2.stop(ctx.currentTime + 0.9);
            } catch (e) {
                // Audio context policy safe ignore
            }
        };

        if (!isMuted) {
            // Play chime safely
            setTimeout(playSuccessChime, 400);
        }
    }

    // 9. Progressive Web App (PWA) Service Worker & Mobile Install
    if ('caches' in window) {
        caches.keys().then((keys) => {
            keys.filter(k => /yogeswari|sesha/i.test(k)).forEach(k => caches.delete(k));
        }).catch(() => {});
    }

    if ('serviceWorker' in navigator) {
        window.addEventListener('load', () => {
            navigator.serviceWorker.register('/sw.js')
                .then((reg) => reg.update().catch(() => {}))
                .catch(() => {});
        });
    }

    let deferredInstallPrompt = null;
    window.addEventListener('beforeinstallprompt', (e) => {
        e.preventDefault();
        deferredInstallPrompt = e;
        const installBtns = document.querySelectorAll('.pwa-install-trigger');
        installBtns.forEach(btn => btn.style.display = 'inline-flex');
    });

    document.querySelectorAll('.pwa-install-trigger').forEach(btn => {
        btn.addEventListener('click', async () => {
            if (deferredInstallPrompt) {
                deferredInstallPrompt.prompt();
                const choiceResult = await deferredInstallPrompt.userChoice;
                if (choiceResult.outcome === 'accepted') {
                    document.querySelectorAll('.pwa-install-trigger').forEach(b => b.style.display = 'none');
                }
                deferredInstallPrompt = null;
            }
        });
    });

    // Continuous Add to Cart (AJAX) with live cart badge and quick View Cart / Checkout bar
    function updateCartBadges(totalCount) {
        const badges = document.querySelectorAll('#header-cart-badge, a[title="Shopping Cart"] .badge-counter');
        badges.forEach(badge => {
            badge.textContent = String(totalCount);
            badge.classList.remove('badge-pop');
            void badge.offsetWidth;
            badge.classList.add('badge-pop');
            setTimeout(() => badge.classList.remove('badge-pop'), 240);
        });
    }

    function showCartToast({ isError = false, title, sub, cartItemCount = 0 }) {
        const existing = document.getElementById('cart-floating-toast');
        if (existing) {
            existing.remove();
        }

        const toast = document.createElement('div');
        toast.id = 'cart-floating-toast';
        toast.className = 'cart-floating-toast' + (isError ? ' is-error' : '');
        toast.setAttribute('role', 'status');

        const header = document.createElement('div');
        header.className = 'cart-floating-toast-header';

        const textWrap = document.createElement('div');
        const titleEl = document.createElement('div');
        titleEl.className = 'cart-floating-toast-title';
        titleEl.textContent = title;
        textWrap.appendChild(titleEl);

        if (sub) {
            const subEl = document.createElement('div');
            subEl.className = 'cart-floating-toast-sub';
            subEl.textContent = sub;
            textWrap.appendChild(subEl);
        }

        const closeBtn = document.createElement('button');
        closeBtn.type = 'button';
        closeBtn.className = 'cart-floating-toast-close';
        closeBtn.setAttribute('aria-label', 'Dismiss cart notification');
        closeBtn.textContent = '×';
        closeBtn.addEventListener('click', () => toast.remove());

        header.appendChild(textWrap);
        header.appendChild(closeBtn);
        toast.appendChild(header);

        if (!isError) {
            const actions = document.createElement('div');
            actions.className = 'cart-floating-toast-actions';

            const viewCartLink = document.createElement('a');
            viewCartLink.href = '/cart';
            viewCartLink.className = 'btn btn-primary btn-sm';
            viewCartLink.textContent = 'View Cart (' + cartItemCount + ') →';

            const checkoutLink = document.createElement('a');
            checkoutLink.href = '/checkout';
            checkoutLink.className = 'btn btn-buy-now btn-sm';
            checkoutLink.textContent = 'Checkout →';

            actions.appendChild(viewCartLink);
            actions.appendChild(checkoutLink);
            toast.appendChild(actions);
        }

        document.body.appendChild(toast);
    }

    function resolveProductTitleFromForm(form) {
        const card = form.closest('.product-card, .deal-card');
        if (card) {
            const titleEl = card.querySelector('.card-title, h3, h4');
            if (titleEl && titleEl.textContent.trim()) {
                return titleEl.textContent.trim();
            }
        }
        const pageTitle = document.querySelector('h1');
        if (pageTitle && pageTitle.textContent.trim() && window.location.pathname.indexOf('/products/') !== -1) {
            return pageTitle.textContent.trim();
        }
        return 'Item';
    }

    document.addEventListener('submit', async function (e) {
        const form = e.target;
        if (!form || !form.matches('form.js-cart-form, form[action$="/cart/add"]')) {
            return;
        }

        // Let Buy Now submit normally to /cart/buy-now -> /checkout
        const submitter = e.submitter;
        if (submitter) {
            const formAction = submitter.getAttribute('formaction') || '';
            if (formAction.indexOf('buy-now') !== -1 || submitter.classList.contains('js-buy-now-btn')) {
                return;
            }
        }

        e.preventDefault();

        const addBtn = form.querySelector('.js-add-to-cart-btn') || form.querySelector('button[type="submit"]');
        const labelEl = addBtn ? (addBtn.querySelector('.js-btn-label') || addBtn.querySelector('span')) : null;
        const actionUrl = form.getAttribute('action') || '/cart/add';
        const formData = new FormData(form);
        const encodedBody = new URLSearchParams(formData).toString();
        const productTitle = resolveProductTitleFromForm(form);

        if (addBtn) {
            addBtn.setAttribute('aria-busy', 'true');
        }

        try {
            const response = await fetch(actionUrl, {
                method: 'POST',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                },
                credentials: 'same-origin',
                body: encodedBody
            });

            if (response.redirected && response.url && response.url.indexOf('/login') !== -1) {
                window.location.href = response.url;
                return;
            }

            const contentType = response.headers.get('content-type') || '';
            if (!contentType.includes('application/json')) {
                if (response.status === 403) {
                    window.location.reload();
                } else {
                    window.location.href = '/login';
                }
                return;
            }

            const data = await response.json();
            if (!response.ok || !data.success) {
                showCartToast({
                    isError: true,
                    title: 'Could not add item',
                    sub: (data && data.message) ? data.message : 'Stock limit reached or unavailable.'
                });
                return;
            }

            updateCartBadges(data.cartItemCount);

            if (addBtn) {
                addBtn.classList.add('is-added');
                const isLarge = addBtn.classList.contains('btn-lg');
                const nextText = isLarge
                    ? '✓ In Cart (' + data.productQuantity + ') · Add More'
                    : '✓ Added (' + data.productQuantity + ') +1';
                if (labelEl) {
                    labelEl.textContent = nextText;
                } else {
                    addBtn.textContent = nextText;
                }
            }

            showCartToast({
                isError: false,
                title: '✓ Added ' + productTitle,
                sub: 'In Cart: ' + data.productQuantity + ' unit(s) · Total Cart: ' + data.cartItemCount + ' item(s)',
                cartItemCount: data.cartItemCount
            });
        } catch (err) {
            console.error('Add to cart error:', err);
            showCartToast({
                isError: true,
                title: 'Connection error',
                sub: 'Please check your connection and try again.'
            });
        } finally {
            if (addBtn) {
                addBtn.removeAttribute('aria-busy');
            }
        }
    });
});


